package com.mparticle.kits;

import android.text.TextUtils;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.commerce.Impression;
import com.mparticle.commerce.Product;
import com.mparticle.commerce.TransactionAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Defines;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.ProductCategory;

/**
 * Created by sojanpr on 4/11/18.
 * <p>
 * Class for Branch utility methods to convert MParticle events to Branch events
 * </p>
 */

class BranchUtil {
    enum MPEventKeys {
        position,
        amount,
        screen_name,
        impression,
        product_list_name,
        product_list_Source,
        checkout_options,
        checkout_step,

    }

    enum ExtraBranchEventKeys {
        product_category
    }

    private final HashMap<String, BRANCH_STANDARD_EVENT> BranchMParticleEventNames;

    BranchUtil() {
        BranchMParticleEventNames = new HashMap<>();
        // Mapping MParticle Commerce Event names to possible matches in Branch events
        BranchMParticleEventNames.put(Product.ADD_TO_CART, BRANCH_STANDARD_EVENT.ADD_TO_CART);
        BranchMParticleEventNames.put(Product.ADD_TO_WISHLIST, BRANCH_STANDARD_EVENT.ADD_TO_WISHLIST);
        BranchMParticleEventNames.put(Product.CHECKOUT, BRANCH_STANDARD_EVENT.INITIATE_PURCHASE);
        BranchMParticleEventNames.put(Product.CLICK, BRANCH_STANDARD_EVENT.VIEW_ITEM);
        BranchMParticleEventNames.put(Product.PURCHASE, BRANCH_STANDARD_EVENT.PURCHASE);
        BranchMParticleEventNames.put(Product.DETAIL, BRANCH_STANDARD_EVENT.VIEW_ITEM);
        BranchMParticleEventNames.put(Product.CHECKOUT_OPTION, BRANCH_STANDARD_EVENT.INITIATE_PURCHASE);

        BranchMParticleEventNames.put(MParticle.EventType.Search.name(), BRANCH_STANDARD_EVENT.SEARCH);
        BranchMParticleEventNames.put(MParticle.EventType.Transaction.name(), BRANCH_STANDARD_EVENT.PURCHASE);
        BranchMParticleEventNames.put(MParticle.EventType.UserContent.name(), BRANCH_STANDARD_EVENT.VIEW_ITEM);
    }


    /**
     * Get a matching {@link BRANCH_STANDARD_EVENT} for the MParticle event name provided
     *
     * @param mParticleEventName {@link String} MParticle event name
     * @return {@link BRANCH_STANDARD_EVENT} if there a matching event for the given MParticle event
     */
    private BRANCH_STANDARD_EVENT getBranchStandardEvent(String mParticleEventName) {
        return BranchMParticleEventNames.get(mParticleEventName);
    }

    /**
     * Translate the given MPEvent / Commerce event to {@link ProductCategory} and add to the BUO
     *
     * @param buo          BUO representing content for the event
     * @param categoryName MPEvent / Commerce event category
     */
    private static void translateEventCategory(BranchUniversalObject buo, String categoryName) {
        ProductCategory category = ProductCategory.getValue(categoryName);
        if (category != null) {
            buo.getContentMetadata().setProductCategory(category);
        } else {
            buo.getContentMetadata().addCustomMetadata(BranchUtil.ExtraBranchEventKeys.product_category.name(), categoryName);
        }
    }

    private BranchEvent createBranchEventFromEventName(String eventName) {
        BranchEvent branchEvent;
        BRANCH_STANDARD_EVENT branchStandardEvent = getBranchStandardEvent(eventName);
        if (branchStandardEvent != null) {
            branchEvent = new BranchEvent(branchStandardEvent);
        } else {
            branchEvent = new BranchEvent(eventName.toUpperCase());
        }
        return branchEvent;
    }

    static class MapReader {
        private final Map<String, String> mapObj;

        MapReader(Map<String, String> mapObj) {
            this.mapObj = new HashMap<>(mapObj);
        }

        String readOutString(String key) {
            return mapObj.remove(key);
        }

        Double readOutDouble(String key) {
            Double val = null;
            try {
                val = Double.parseDouble(mapObj.get(key));
                mapObj.remove(key);
            } catch (Exception ignore) {
            }
            return val;
        }

        Map<String, String> getMap() {
            return mapObj;
        }
    }

    // Region Translate MPEvents

    BranchEvent createBranchEventFromMPEvent(MPEvent mpEvent) {
        BranchEvent branchEvent = createBranchEventFromEventName(mpEvent.getEventType().name());
        BranchUniversalObject buo = new BranchUniversalObject();
        branchEvent.addContentItems(buo);
        // Apply event category
        if (!TextUtils.isEmpty(mpEvent.getCategory())) {
            BranchUtil.translateEventCategory(buo, mpEvent.getCategory());
        }
        // Apply event name
        if (!TextUtils.isEmpty(mpEvent.getEventName())) {
            buo.setTitle(mpEvent.getEventName());
        }
        if (mpEvent.getInfo() != null) {
            updateEventWithInfo(branchEvent, buo, mpEvent.getInfo());
        }
        return branchEvent;
    }

    private void updateEventWithInfo(BranchEvent event, BranchUniversalObject buo, Map<String, String> info) {
        BranchUtil.MapReader mapReader = new BranchUtil.MapReader(info);

        // Affiliation
        String stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_AFFILIATION);
        if (!TextUtils.isEmpty(stringValue)) {
            event.setAffiliation(stringValue);
        }

        // Shipping
        Double doubleVal = mapReader.readOutDouble(CommerceEventUtils.Constants.ATT_SHIPPING);
        if (doubleVal != null) {
            event.setShipping(doubleVal);
        }
        // Tax
        doubleVal = mapReader.readOutDouble(CommerceEventUtils.Constants.ATT_TAX);
        if (doubleVal != null) {
            event.setTax(doubleVal);
        }
        // Transaction ID
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_TRANSACTION_ID);
        if (!TextUtils.isEmpty(stringValue)) {
            event.setTransactionID(stringValue);
        }
        // Quantity
        doubleVal = mapReader.readOutDouble(CommerceEventUtils.Constants.ATT_PRODUCT_QUANTITY);
        if (doubleVal != null) {
            buo.getContentMetadata().setQuantity(doubleVal);
        }
        // Variant
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_VARIANT);
        if (!TextUtils.isEmpty(stringValue)) {
            buo.getContentMetadata().setProductVariant(stringValue);
        }
        // Product ID
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_ID);
        if (!TextUtils.isEmpty(stringValue)) {
            buo.setCanonicalIdentifier(stringValue);
        }
        // Product Name
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_NAME);
        if (!TextUtils.isEmpty(stringValue)) {
            buo.getContentMetadata().setProductName(stringValue);
        }
        // Category
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_CATEGORY);
        if (!TextUtils.isEmpty(stringValue)) {
            BranchUtil.translateEventCategory(buo, stringValue);
        }
        // Brand
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_BRAND);
        if (!TextUtils.isEmpty(stringValue)) {
            buo.getContentMetadata().setProductBrand(stringValue);
        }
        // Coupon
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_PRODUCT_COUPON_CODE);
        if (!TextUtils.isEmpty(stringValue)) {
            event.setCoupon(stringValue);
        }
        // Price
        doubleVal = mapReader.readOutDouble(CommerceEventUtils.Constants.ATT_PRODUCT_PRICE);
        if (doubleVal != null) {
            buo.getContentMetadata().setPrice(doubleVal, null);
        }
        // Revenue
        doubleVal = mapReader.readOutDouble(CommerceEventUtils.Constants.ATT_PRODUCT_TOTAL_AMOUNT);
        if (doubleVal != null) {
            event.setRevenue(doubleVal);
        }
        // Currency
        stringValue = mapReader.readOutString(CommerceEventUtils.Constants.ATT_ACTION_CURRENCY_CODE);
        if (!TextUtils.isEmpty(stringValue)) {
            event.setCurrency(CurrencyType.getValue(stringValue));
        }
        // Add all other key values to custom metadata
        updateBranchEventWithCustomData(event, mapReader.getMap());
    }

    // End Region Translate MPEvents


    // Region Translate CommerceEvents

    BranchEvent createBranchEventFromMPCommerceEvent(CommerceEvent event) {
        BranchEvent branchEvent = createBranchEventFromEventName(event.getProductAction().toLowerCase());
        // Add all the products in the product list to Branch event
        if (event.getProducts() != null) {
            Map<String, String> additionalMetadata = new HashMap<>();
            if (!TextUtils.isEmpty(event.getProductListName())) {
                additionalMetadata.put(BranchUtil.MPEventKeys.product_list_name.name(), event.getProductListName());
            }
            if (!TextUtils.isEmpty(event.getProductListSource())) {
                additionalMetadata.put(BranchUtil.MPEventKeys.product_list_Source.name(), event.getProductListSource());
            }

            addProductListToBranchEvent(branchEvent, event.getProducts(), event, additionalMetadata);
        }

        // Add all impressions to the Branch Event
        if (event.getImpressions() != null) {
            for (Impression impression : event.getImpressions()) {
                if (impression.getProducts() != null) {
                    Map<String, String> additionalMetadata = new HashMap<>();
                    if (!TextUtils.isEmpty(impression.getListName())) {
                        additionalMetadata.put(BranchUtil.MPEventKeys.impression.name(), impression.getListName());
                    }
                    addProductListToBranchEvent(branchEvent, impression.getProducts(), event, additionalMetadata);
                }
            }
        }
        if (event.getTransactionAttributes() != null) {
            updateBranchEventWithTransactionAttributes(branchEvent, event.getTransactionAttributes());
        }
        if (!TextUtils.isEmpty(event.getProductListName())) {
            branchEvent.addCustomDataProperty(BranchUtil.MPEventKeys.product_list_name.name(), event.getProductListName());
        }
        if (!TextUtils.isEmpty(event.getProductListSource())) {
            branchEvent.addCustomDataProperty(BranchUtil.MPEventKeys.product_list_Source.name(), event.getProductListSource());
        }
        if (!TextUtils.isEmpty(event.getCheckoutOptions())) {
            branchEvent.addCustomDataProperty(BranchUtil.MPEventKeys.checkout_options.name(), event.getCheckoutOptions());
        }
        if (!TextUtils.isEmpty(event.getScreen())) {
            branchEvent.addCustomDataProperty(MPEventKeys.screen_name.name(), event.getScreen());
        }
        if (event.getCheckoutStep() != null) {
            try {
                branchEvent.addCustomDataProperty(MPEventKeys.checkout_step.name(), event.getCheckoutStep().toString());
            } catch (Exception ignore) {
            }
        }
        if (!TextUtils.isEmpty(event.getCurrency())) {
            branchEvent.setCurrency(CurrencyType.getValue(event.getCurrency()));
        }
        return branchEvent;
    }

    private void addProductListToBranchEvent(BranchEvent branchEvent, List<Product> products, CommerceEvent event, Map<String, String> additionalMetadata) {
        if (products != null) {

            for (Product product : products) {
                branchEvent.addContentItems(createBranchUniversalObjectFromMProduct(product, event, additionalMetadata));
            }
        }
    }

    private BranchUniversalObject createBranchUniversalObjectFromMProduct(Product product, CommerceEvent event, Map<String, String> additionalMetadata) {
        BranchUniversalObject buo = new BranchUniversalObject();
        if (!TextUtils.isEmpty(product.getBrand())) {
            buo.getContentMetadata().setProductBrand(product.getBrand());
        }
        if (!TextUtils.isEmpty(product.getCategory())) {
            BranchUtil.translateEventCategory(buo, product.getCategory());
        }
        if (!TextUtils.isEmpty(product.getCouponCode())) {
            buo.getContentMetadata().addCustomMetadata(Defines.Jsonkey.Coupon.getKey(), product.getCouponCode());
        }
        if (!TextUtils.isEmpty(product.getName())) {
            buo.getContentMetadata().setProductName(product.getName());
        }
        if (!TextUtils.isEmpty(product.getVariant())) {
            buo.getContentMetadata().setProductVariant(product.getVariant());
        }
        if (!TextUtils.isEmpty(product.getSku())) {
            buo.getContentMetadata().setSku(product.getSku());
        }
        if (product.getPosition() != null) {
            buo.getContentMetadata().addCustomMetadata(BranchUtil.MPEventKeys.position.name(), Integer.toString(product.getPosition()));
        }
        buo.getContentMetadata().setPrice(product.getUnitPrice(), CurrencyType.getValue(event.getCurrency()));
        buo.getContentMetadata().setQuantity(product.getQuantity());
        buo.getContentMetadata().addCustomMetadata(BranchUtil.MPEventKeys.amount.name(), Double.toString(product.getTotalAmount()));
        if (product.getCustomAttributes() != null) {
            addCustomDataToBranchUniversalObject(buo, product.getCustomAttributes());
        }
        if (additionalMetadata != null) {
            addCustomDataToBranchUniversalObject(buo, additionalMetadata);
        }
        return buo;
    }

    private void addCustomDataToBranchUniversalObject(BranchUniversalObject buo, Map<String, String> customAttr) {
        ContentMetadata contentMetadata = buo.getContentMetadata();
        for (String key : customAttr.keySet()) {
            contentMetadata.addCustomMetadata(key, customAttr.get(key));
        }
    }

    private void updateBranchEventWithTransactionAttributes(BranchEvent event, TransactionAttributes transAttr) {
        if (!TextUtils.isEmpty(transAttr.getAffiliation())) {
            event.setAffiliation(transAttr.getAffiliation());
        }
        if (!TextUtils.isEmpty(transAttr.getCouponCode())) {
            event.setCoupon(transAttr.getCouponCode());
        }
        if (!TextUtils.isEmpty(transAttr.getId())) {
            event.setTransactionID(transAttr.getId());
        }
        if (transAttr.getRevenue() != null) {
            event.setRevenue(transAttr.getRevenue());
        }
        if (transAttr.getShipping() != null) {
            event.setShipping(transAttr.getShipping());
        }
        if (transAttr.getTax() != null) {
            event.setTax(transAttr.getTax());
        }
    }

    void updateBranchEventWithCustomData(BranchEvent branchEvent, Map<String, String> eventAttributes) {
        for (String key : eventAttributes.keySet()) {
            branchEvent.addCustomDataProperty(key, eventAttributes.get(key));
        }
    }

    // End Region Translate CommerceEvents

}
