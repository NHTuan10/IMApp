-module(mod_gcm).
-author("TuanNguyen").

-include("ejabberd.hrl").
-include("logger.hrl").
-include("jlib.hrl").

-behaviour(gen_mod).

-record(gcm_users,{user,gcm_key,last_modified}).

-define(GCM_SERVER,"https://gcm-http.googleapis.com/gcm").
-define(GCM_URL,?GCM_SERVER ++ "/send").
-define(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8").


-export([start/2,stop/1,url_encode/1,handle_message/3,register_iq/3,send/2]).

%%URI encode
%% reference: http://stackoverflow.com/questions/114196/url-encode-in-erlang
escape_uri(S) when is_list(S) ->
    escape_uri(unicode:characters_to_binary(S));
escape_uri(<<C:8, Cs/binary>>) when C >= $a, C =< $z ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) when C >= $A, C =< $Z ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) when C >= $0, C =< $9 ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) when C == $. ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) when C == $- ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) when C == $_ ->
    [C] ++ escape_uri(Cs);
escape_uri(<<C:8, Cs/binary>>) ->
    escape_byte(C) ++ escape_uri(Cs);
escape_uri(<<>>) ->
    "".

escape_byte(C) ->
    "%" ++ hex_octet(C).

hex_octet(N) when N =< 9 ->
    [$0 + N];
hex_octet(N) when N > 15 ->
    hex_octet(N bsr 4) ++ hex_octet(N band 15);
hex_octet(N) ->
    [N - 10 + $a].

%% URL encode 
url_encode(Data) ->
	url_encode(Data,"").

url_encode([],Aux) ->
	Aux;

url_encode([{Key,Value}|Rest],"") ->
	url_encode(Rest,escape_uri(Key) ++ "=" ++ escape_uri(Value));

url_encode([{Key,Value}|Rest],Aux) ->
	url_encode(Rest, Aux ++ "&" ++ escape_uri(Key) ++ "=" ++ escape_uri(Value)).


%% Example for GET & POST request
%% httpc:request(get, {"http://www.erlang.org", []}, [], []).
%% httpc:request(post,{"http://posttestserver.com/post.php",[{"data","anhoi"}],"application/x-www-form-urlencoded","hahabodyday"},[],[]).
%15> {ok, {{Version, 200, ReasonPhrase}, Headers, Body}} =
%%15>       httpc:request(get, {"http://www.erlang.org", []}, [], []).

%% HTTP Post to Google CGM API and handle response
send([{Key,Value}|Rest],API_KEY) ->
	Headers = [{"Authorization",url_encode([{"key",API_KEY}])}],
	Body = url_encode([{Key,Value}|Rest]),
	?INFO_MSG("mod_gcm: GCM send message, Key: ~p~n Body: ~p",[API_KEY,Body]),
	ssl:start(),
	application:start(inets),
	{ok,{{_,Code,Status},_,ResponseBody}} = httpc:request(post,{?GCM_URL,Headers,?CONTENT_TYPE,Body},[],[]),
	case catch Code of 
		200 -> ?INFO_MSG("mod_gcm: The message was sent ~n~p",[ResponseBody]);
		_ -> ?INFO_MSG("mod_gcm: ~s : ~s",[Status,ResponseBody])
	end.	

%%Handle offline message package and push it to user via GCM
handle_message(From,To,Packet) ->
	Type = fxml:get_tag_attr_s(<<"type">>,Packet),
	?INFO_MSG("mod_gcm: Offline message from: ~p~n To: ~p~n Packet:~p",[From,To,Packet]),

	case catch Type of 
		"normal" ->
			ok;
		MsgType ->
			?INFO_MSG("mod_gcm: Offline Message Type: ~s",[MsgType]),
			StrFrom = jlib:jid_to_string(From#jid{resource = <<"">>}),
			StrTo   = jlib:jid_to_string(To#jid{resource = <<"">>}),
			?INFO_MSG("mod_gcm: Offline message from: ~p~n To: ~p~n",[StrFrom,StrTo]),
			ToUser = To#jid.user,
			ToServer = To#jid.server,
			Body = fxml:get_path_s(Packet, [{elem, <<"body">>}, cdata]),
			?INFO_MSG("mod_gcm: Offline Message Body: ~p",[Body]),
			{Subscription, _Groups} = 
				ejabberd_hooks:run_fold(roster_get_jid_info, ToServer, {none, []}, [ToUser, ToServer, From]),
			?INFO_MSG("mod_gcm: Subscription is ~p",[Subscription]),
				
			case Subscription of 
				both -> 
					case catch Body of
						<<>> -> ok; 
						_ 	->
							case catch mnesia:dirty_read(gcm_users,{ToUser,ToServer}) of
								[] -> 
									?INFO_MSG("mod_gcm: No registration user record for ~p@~p",[ToUser,ToServer]);

								[#gcm_users{gcm_key=Token}] ->
									Args = [{"to",Token},{"data.message",Body},{"data.source",StrFrom},{"data.destination",StrTo}],
									?INFO_MSG("mod_gcm: Arguments for for gcm: ~p",[Args]),
									send(Args,ejabberd_config:get_global_option(gcm_api_key, fun(V) -> V end))
							end
					end;
				_ -> ok	
			end						
	end.	

register_iq(#jid{user=User,server=Server}=From,To,#iq{type =Type,sub_el=SubELement} = IQ)->
	LUser = jlib:nodeprep(User),
	LServer = jlib:nameprep(Server),
	{MegaSeconds,Seconds,MilliSecs} = now(),
	TimeStamp = MegaSeconds * 1000000 + Seconds,
	?INFO_MSG("Type: ~p",[Type]),
	Token = fxml:get_tag_cdata(fxml:get_subtag(SubELement,<<"token">>)),
	?INFO_MSG("Token: ~p",[Token]),
	Fun = 	fun() -> 
				mnesia:write(#gcm_users{user={LUser,LServer},gcm_key=Token,last_modified=TimeStamp})
		  	end,
	case catch mnesia:dirty_read(gcm_users,{LUser,LServer}) of
		[] -> 
			mnesia:transaction(Fun),
			?INFO_MSG("mod_gcm: Create new user registration record: ~p@~p, Token: ~p",[LUser,LServer,Token]);

		[#gcm_users{user={LUser,LServer},gcm_key=Token}] ->	
			mnesia:transaction(Fun),
			?INFO_MSG("mod_gcm: Update last modified for user: ~p@~p",[LUser,LServer]);

		[#gcm_users{user={LUser,LServer},gcm_key=_}] ->	
			mnesia:transaction(Fun),
			?INFO_MSG("mod_gcm: Update token for user: ~p@~p, Token: ~p",[LUser,LServer,Token])
		
	end,
	IQ#iq{type=result, sub_el=[]}.


start(Host,Opts) ->
	case catch ejabberd_config:get_global_option(gcm_api_key, fun(V) -> V end) of
		undefined -> ?ERROR_MSG("mod_gcm: No API Key found",[]);
		_ ->
			mnesia:create_table(gcm_users,[{disc_copies, [node()]},{attributes,record_info(fields,gcm_users)}]),
			gen_iq_handler:add_iq_handler(ejabberd_local, Host,<<?GCM_SERVER>>, ?MODULE, register_iq, no_queue),
			ejabberd_hooks:add(offline_message_hook, Host, ?MODULE, handle_message, 49),
			?INFO_MSG("MOD GCM: Started successful!!",[]),
			ok
	end.	
	


stop(Host) ->
	?INFO_MSG("MOD GCM: Stopped!",[]),
	ok.
