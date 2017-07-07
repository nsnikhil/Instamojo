package com.nrs.nsnik.instamojo.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.instamojo.android.Instamojo;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.models.Errors;
import com.instamojo.android.models.Order;
import com.instamojo.android.network.Request;
import com.nrs.nsnik.instamojo.Objects.AccessToken;
import com.nrs.nsnik.instamojo.R;
import com.nrs.nsnik.instamojo.interfaces.RetroFitCalls;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PaymentFragment extends Fragment {

    @BindView(R.id.paymentName)
    EditText mName;
    @BindView(R.id.paymentEmail)
    EditText mEmail;
    @BindView(R.id.paymentAmount)
    EditText mAmount;
    @BindView(R.id.paymentCurrency)
    EditText mCurrency;
    @BindView(R.id.paymentDescription)
    EditText mDes;
    @BindView(R.id.paymentPhone)
    EditText mPhone;
    @BindView(R.id.paymentDo)
    Button mPay;

    private static final String TAG = PaymentFragment.class.getSimpleName();

    private Unbinder mUnbinder;

    private static final String mClientTestId = "2ivV4tuZbAm7yI7nfRn5g98Ap31JnwL3aJm38uDW";
    private static final String mClientTestSecretKey = "1PeOsLExG3bG2trfaOLas9JWvOaTsS9wIgcA2Qzr7IU6GNgevQx8bB7JfUFLEXAcAiftKJ1WrKp6nkbRUpEjCrMleSYZnj6RUq772amPJAhI915xACbOeELFTBA23ncT";

    private static final String mClientId = "g0RrNtQZYPzitKh4vse5PDdncnaizDiLAUc4RfBZ";
    private static final String mClientSecretKey = "tsObPjget7LRGvY3jtGnontm9EDDudSzGbsuL8H5WNHANaRdjZCbPplmnuxYsy7N79M1y8UW1FVtiRrE45OQKUpIiNYIkzh2MsRFpHY519dTpzD7lZC3UHtYjVRSSksN";

    private static final String mRedirectUrl = " https://test.instamojo.com/integrations/android/redirect/";
    private static final String mWebHookUrl = "";

    private static final String  mOathBaseUrl = "https://test.instamojo.com/";

    private Retrofit mRetrofit;

    private Dialog mWaitDialog;


    public PaymentFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_payment, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuFragInsFake:
                setFakeValues();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFakeValues() {
        mName.setText("Temp Name");
        mEmail.setText("tempname@mailinator.com");
        mPhone.setText("9123456789");
        mAmount.setText("2500");
        mDes.setText("test description");
    }

    private void initialize() {
        Instamojo.initialize(getActivity());
        Instamojo.setBaseUrl("https://test.instamojo.com/");
        Instamojo.setLogLevel(Log.DEBUG);
        mWaitDialog =  getWaitDialog().create();
    }

    private void listeners() {
        mPay.setOnClickListener(view -> getAccessToken());
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", mClientTestId);
        params.put("client_secret", mClientTestSecretKey);
        params.put("grant_type","client_credentials");
        return params;
    }

    private void getAccessToken(){
        mWaitDialog.show();
        RetroFitCalls apiClass = getRetrofit().create(RetroFitCalls.class);
        apiClass.getAuthToken(getParams()).enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                if (response.body() != null) {
                    AccessToken token  = response.body();
                    if (token != null) {
                        Log.d(TAG,token.access_token);
                        createOrder(token.access_token);
                    }else {
                        mWaitDialog.dismiss();
                        Log.d(TAG, "token null");
                    }
                }else {
                    mWaitDialog.dismiss();
                    Log.d(TAG, "null");
                }
            }
            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                mWaitDialog.dismiss();
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private Retrofit getRetrofit() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        if (mRetrofit == null) {
            mRetrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(mOathBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofit;
    }

    private void createOrder(String accessToken) {
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String phone = mPhone.getText().toString();
        String amount = mAmount.getText().toString();
        String description = mDes.getText().toString();
        String currency = mCurrency.getText().toString();
        String transactionID = String.valueOf(generateId());
        Order order = new Order(accessToken, transactionID, name, email, phone, amount, description);

        order.setRedirectionUrl(mRedirectUrl);
        order.setCurrency(currency);

        if (isOrderValid(order)) {
            createRequest(order);
        }
    }

    private boolean isOrderValid(Order order) {
        if (!order.isValid()) {
            mWaitDialog.dismiss();
            if (!order.isValidName()) {
                Log.e(TAG, "Buyer name is invalid");
            }
            if (!order.isValidEmail()) {
                Log.e(TAG, "Buyer email is invalid");
            }
            if (!order.isValidPhone()) {
                Log.e(TAG, "Buyer phone is invalid");
            }
            if (!order.isValidAmount()) {
                Log.e(TAG, "Amount is invalid");
            }
            if (!order.isValidDescription()) {
                Log.e(TAG, "description is invalid");
            }
            if (!order.isValidTransactionID()) {
                Log.e(TAG, "Transaction ID is invalid");
            }
            if (!order.isValidRedirectURL()) {
                Log.e(TAG, "Redirection URL is invalid");
            }
            if (!order.isValidWebhook()) {
                Toast.makeText(getActivity(), "Webhook URL is invalid", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private AlertDialog.Builder getWaitDialog(){
        return new AlertDialog.Builder(getActivity()).setMessage(getActivity().getResources().getString(R.string.justaSec));
    }

    private void createRequest(Order order) {
        Request request = new Request(order, (order1, error) -> {
            mWaitDialog.dismiss();
            if (error != null) {
                if (error instanceof Errors.ConnectionError) {
                    Log.e(TAG, "No internet connection");
                } else if (error instanceof Errors.ServerError) {
                    Log.e(TAG, "Server Error. Try again");
                } else if (error instanceof Errors.AuthenticationError) {
                    Log.e(TAG, "Access token is invalid or expired");
                } else if (error instanceof Errors.ValidationError) {
                    Errors.ValidationError validationError = (Errors.ValidationError) error;
                    if (!validationError.isValidTransactionID()) {
                        Log.e(TAG, "Transaction ID is not Unique");
                        return;
                    }if (!validationError.isValidRedirectURL()) {
                        Log.e(TAG, "Redirect url is invalid");
                        return;
                    }if (!validationError.isValidWebhook()) {
                        Toast.makeText(getActivity(), "Webhook url is invalid", Toast.LENGTH_SHORT).show();
                        return;
                    }if (!validationError.isValidPhone()) {
                        Log.e(TAG, "Buyer's Phone Number is invalid/empty");
                        return;
                    }if (!validationError.isValidEmail()) {
                        Log.e(TAG, "Buyer's Email is invalid/empty");
                        return;
                    }if (!validationError.isValidAmount()) {
                        Log.e(TAG, "Amount is either less than Rs.9 or has more than two decimal places");
                        return;
                    }if (!validationError.isValidName()) {
                        Log.e(TAG, "Buyer's Name is required");
                        return;
                    }
                } else {
                    Log.e(TAG, error.getMessage());
                }
                return;
            }
            startPreCreatedUI(order1);
        });
        request.execute();
    }

    private void startPreCreatedUI(Order order) {
        Intent intent = new Intent(getActivity(), PaymentDetailsActivity.class);
        intent.putExtra(Constants.ORDER, order);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
            String orderID = data.getStringExtra(Constants.ORDER_ID);
            String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
            String paymentID = data.getStringExtra(Constants.PAYMENT_ID);
            if (orderID != null && transactionID != null && paymentID != null) {
                //Check for Payment status with Order ID or Transaction ID
                Log.d(TAG, "Check for Payment status with Order ID or Transaction ID");
            } else {
                //Payment was cancelled
                Log.d(TAG, "Payment was cancelled");
            }
        }
    }

    private int generateId() {
        return new Random().nextInt((9999 - 1111) + 1) + 1111;
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }
}
