
Overview

1.1 Module GCM
	This is a plugin that we write in Erlang for ejabberd XMPP Server to push offline message
to devices via Google Cloud Messaging (GCM). The source code is mod_gcm.erl file in folder mod_gcm/src. This source code should
be compiled with the ejabberd source code, then copy the compiled mod_gcm.beam file to
the library folder of ejabberd to work.
Ejabberd source code: https://github.com/processone/ejabberd

1.2 Application source code
	Source code and classes of the application in folder:
IMApp/app/src/main/java/com/gmail/tuannguyen/imapp
Those classes are organized as following packages:

	Package chat: including activity, classes for chat view, sending and receiving chat messages. It also includes implementation for secret session feature.

	Package connection: handle connection, send and receive packages between client and server.

	Package contact: handle contact view, friend request and retrieve contact information.

	Package db: database tasks for storing, insert, query and encrypt messages

	Package gcm: For Google Cloud Messaging communication and offline message notifications

	Package recent: recent conversation feature

	Package security: for security features including account management and key encryption storage

	Package setting: settings

	Package util: including common constants and methods are used by other classes

	Resources folder, including layout, menu, UI and some configurations for the app: IMApp/app/src/main/res/

	Memorizing Trust Manager (The MIT license Copyright (c) 2010 Georg Lukas <georg@op-co.de>) (https://github.com/ge0rg/MemorizingTrustManager): IMApp/MemorizingTrustManager

	SQLCipher library © 2017, Zetetic, LLC (https://www.zetetic.net/about/): IMApp/app/src/main/jniLibs and IMApp/app/libs
