package com.gmail.tuannguyen.imapp.security;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_PASSWORD;

/**
 * Created by tuannguyen on 5/3/16.
 */
public class Accounts {
    public static final String TAG = "Accounts";
    public static final String ACCOUNT_TYPE = "com.gmail.tuannguyen.imapp.auth";
    public static final String KEY_ACCOUNT = "Account";

    public static void addAccount(Context context, Intent intent) {
        String accountName = intent.getStringExtra(KEY_ACCOUNT_NAME);
        String password = intent.getStringExtra(KEY_PASSWORD);
        addAccount(context, accountName, password);
    }

    public static void addAccount(Context context, @NonNull String accountName, @NonNull String password) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        Account account = new Account(accountName, ACCOUNT_TYPE);
        if (accounts.length > 0) {
            if (!accountName.equals(accounts[0].name)) {
                //accountManager.removeAccountExplicitly(accounts[0]);
                //accountManager.removeAccount(accounts[0], null, null);
                removeAllAccounts(context);
                accountManager.addAccountExplicitly(account, password, null);
                return;
            }
            //if exist account is the same as the logged in account
            accountManager.setPassword(account, password);
        } else {
            accountManager.addAccountExplicitly(account, password, null);
        }

    }

    public static void removeAllAccounts(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            accountManager.removeAccount(account, null, null);
        }
    }

    public static Bundle getAccount(Context context) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent();

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        if (accounts.length > 0) {
            Account account = accounts[0];
            intent.putExtra(KEY_ACCOUNT_NAME, account.name);
            intent.putExtra(KEY_PASSWORD, accountManager.getPassword(account));
            intent.putExtra(KEY_ACCOUNT_TYPE, account.type);
            bundle.putParcelable(KEY_ACCOUNT, intent);
            return bundle;
        }
        return null;
        //return accounts;
/*
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts){
            String name = account.name;
            String type = account.type;
            String password = "";
            if (account.type.equals(ACCOUNT_TYPE)) {
                password = accountManager.getPassword(account);
            }
            //String describeContents = account.describeContents();
            Log.d(TAG,"Name: "+ name +  " ; Type: " +type + " ; Pass: " + password);
        }*/

    }
}
