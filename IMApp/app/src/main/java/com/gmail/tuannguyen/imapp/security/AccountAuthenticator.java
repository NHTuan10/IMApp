package com.gmail.tuannguyen.imapp.security;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.gmail.tuannguyen.imapp.LoginActivity;
import com.gmail.tuannguyen.imapp.connection.Connection;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static com.gmail.tuannguyen.imapp.util.Common.KEY_IS_ADDING_NEW_ACCOUNT;
/**
 * Created by tuannguyen on 4/17/16.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private Context context;
    private static String TAG;
    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KEY_ACCOUNT_TYPE, accountType);
        //intent.putExtra(KEY_AUTH_TYPE, authTokenType);
        intent.putExtra(KEY_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, android.accounts.Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, android.accounts.Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        final AccountManager accountManager = AccountManager.get(context);
        String authToken = accountManager.peekAuthToken(account, authTokenType);
        if (TextUtils.isEmpty(authToken)) {
            String password = accountManager.getPassword(account);
            try {
                Log.d(TAG, "Authenticate with exist password");
                Connection.getInstance().login(account.name, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(KEY_ACCOUNT_NAME, account.name);
            result.putString(KEY_ACCOUNT_TYPE, account.type);
            //result.putString(KEY_AUTH_TOKEN, authToken);
            return result;
        }

        //if cannot find authToken
        final Bundle bundle = new Bundle();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KEY_ACCOUNT_TYPE, account.type);
        //intent.putExtra(KEY_AUTH_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * Ask the authenticator for a localized label for the given authTokenType.
     *
     * @param authTokenType the authTokenType whose label is to be returned, will never be null
     * @return the localized label of the auth token type, may be null if the type isn't known
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    /*
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (KEY_AUTH_TYPE_FULL_ACCESS.equals(authTokenType))
            return KEY_AUTH_TYPE_FULL_ACCESS_LABEL;
        return authTokenType;
    }
*/
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, android.accounts.Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, android.accounts.Account account, String[] features) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return bundle;
    }
}

