package com.vemja.driver.wallet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vemja.driver.models.ModelWalletTransactionsResponse;

public class AdapterWalletFragments extends FragmentPagerAdapter {
    ModelWalletTransactionsResponse model;


    public AdapterWalletFragments(FragmentManager fm,ModelWalletTransactionsResponse model) {
        super(fm);
        this.model = model;

    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                return WalletTransactionFragment.newInstance("instance_one",model);

            case 1:
                return WalletTransactionFragment.newInstance("instance_two",model);

            case 2:
                return WalletTransactionFragment.newInstance("instance_three",model);


            default: return WalletTransactionFragment.newInstance("Fragment 1, Default",model);
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0){
            return "All Transactions";
        }else if (position==1){
            return "Money In";
        }else if(position ==2){
            return "Money Out";
        }
        else {
            return "All Transactions";
        }
    }


}