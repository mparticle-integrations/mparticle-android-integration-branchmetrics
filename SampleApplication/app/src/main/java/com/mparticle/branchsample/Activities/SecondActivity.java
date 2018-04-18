package com.mparticle.branchsample.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.branchsample.R;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.commerce.Impression;
import com.mparticle.commerce.Product;
import com.mparticle.commerce.Promotion;
import com.mparticle.commerce.TransactionAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecondActivity extends BaseActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.branch_events);
       
        findViewById(R.id.cmdTrackEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logCommerceEvent((String) ((Spinner) findViewById(R.id.event_name_spinner)).getSelectedItem());
            }
        });
        
        findViewById(R.id.cmdTrackView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logScreen();
            }
        });
        
        findViewById(R.id.cmdLogSimpleEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logSimpleEvent();
            }
        });
    }
    
    @Override
    public String getTextTitle() {
        return "Second Page";
    }
    
    @Override
    public View.OnClickListener getButtonListener() {
        return null;
    }
    
    @Override
    public String getButtonTitle() {
        return "Next Activity";
    }
    
    private void logScreen() {
        Map<String, String> eventInfo = new HashMap<String, String>(2);
        eventInfo.put("custom_attr_key1", "custom_attr_val1");
        eventInfo.put("custom_attr_key2", "custom_attr_val2");
        
        MPEvent event = new MPEvent.Builder("Food Order", MParticle.EventType.Transaction)
                .duration(100)
                .info(eventInfo)
                .category("Delivery")
                .build();
        
        MParticle.getInstance().logScreen("SecondActivity", eventInfo);
    }
    
    private void logSimpleEvent() {
        Map<String, String> eventInfo = new HashMap<String, String>(2);
        eventInfo.put("custom_attr_key1", "custom_attr_val1");
        eventInfo.put("custom_attr_key2", "custom_attr_val2");
        
        MPEvent event = new MPEvent.Builder("Simple Event", MParticle.EventType.Transaction)
                .duration(100)
                .info(eventInfo)
                .category("Food and Beverages")
                .build();
        MParticle.getInstance().logEvent(event);
    }
    
    private void logCommerceEvent(String eventName) {
        
        Map<String, String> customAttr = new HashMap<String, String>(2);
        customAttr.put("custom_attr_key1", "custom_attr_val1");
        customAttr.put("custom_attr_key2", "custom_attr_val2");
        
        Product product1 = new Product.Builder("Prod1", "my_sku", 100.00)
                .brand("my_prod_brand")
                .category("my_prod_category")
                .couponCode("my_coupon_code")
                .customAttributes(customAttr)
                .name("my_prod_name")
                .position(1)
                .quantity(2.5)
                .sku("my_sku")
                .unitPrice(12.5)
                .variant("my_variant")
                .quantity(4)
                .build();
        
        Product product2 = new Product.Builder("Impression_prod", "my_sku", 100.00)
                .brand("my_prod_brand")
                .category("my_prod_category")
                .couponCode("my_coupon_code")
                .customAttributes(customAttr)
                .name("my_prod_name")
                .position(1)
                .quantity(2.5)
                .sku("my_sku")
                .unitPrice(12.5)
                .variant("my_variant")
                .quantity(4)
                .build();
        
        Product product3 = new Product.Builder("prod3", "my_sku", 100.00)
                .brand("my_prod_brand")
                .category("my_prod_category")
                .couponCode("my_coupon_code")
                .customAttributes(customAttr)
                .name("my_prod_name")
                .position(1)
                .quantity(2.5)
                .sku("my_sku")
                .unitPrice(12.5)
                .variant("my_variant")
                .quantity(4)
                .build();
        
        TransactionAttributes attributes = new TransactionAttributes("foo-transaction-id")
                .setCouponCode("transaction_coupon_code")
                .setAffiliation("transaction_affiliation")
                .setId("transaction_id")
                .setRevenue(13.5)
                .setShipping(3.5)
                .setTax(4.5);
        
        Impression impression = new Impression("Impression", product2);
        
        
        CommerceEvent commerceEvent = new CommerceEvent.Builder(eventName, product1)
                .currency("USD")
                .customAttributes(customAttr)
                .transactionAttributes(attributes)
                .addImpression(impression)
                .productListName("my_commerce_event_prod_list")
                .addProduct(product3)
                .build();
        MParticle.getInstance().logEvent(commerceEvent);
    }
}
