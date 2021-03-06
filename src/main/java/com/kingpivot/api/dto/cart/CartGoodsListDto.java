package com.kingpivot.api.dto.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingpivot.base.objectFeatureItem.model.ObjectFeatureItem;

public class CartGoodsListDto {
    private String id;//主键

    private String cartID;

    private String goodsShopID;//商品店铺ID

    private Double standPrice = 0.00d;//标准价格

    private double discountRate = 0.00d;//折扣比例

    private double priceNow = 0.00d;//实际价格

    private Integer qty = 0;//当前商品数量

    private double priceTotal = 0.00d;//实际总价格

    private String objectFeatureItemID1; //对象特征选项ID1

    private String objectFeatureItemName1; //对象特征选项Name1

    @JsonIgnore
    private ObjectFeatureItem objectFeatureItem1;

    private Integer isSelected = 1;//是否选中: 1是 2否


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCartID() {
        return cartID;
    }

    public void setCartID(String cartID) {
        this.cartID = cartID;
    }

    public String getGoodsShopID() {
        return goodsShopID;
    }

    public void setGoodsShopID(String goodsShopID) {
        this.goodsShopID = goodsShopID;
    }

    public Double getStandPrice() {
        return standPrice;
    }

    public void setStandPrice(Double standPrice) {
        this.standPrice = standPrice;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getPriceNow() {
        return priceNow;
    }

    public void setPriceNow(double priceNow) {
        this.priceNow = priceNow;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public double getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(double priceTotal) {
        this.priceTotal = priceTotal;
    }

    public String getObjectFeatureItemID1() {
        return objectFeatureItemID1;
    }

    public void setObjectFeatureItemID1(String objectFeatureItemID1) {
        this.objectFeatureItemID1 = objectFeatureItemID1;
    }

    public String getObjectFeatureItemName1() {
        return objectFeatureItemName1;
    }

    public void setObjectFeatureItemName1(String objectFeatureItemName1) {
        this.objectFeatureItemName1 = objectFeatureItemName1;
    }

    public ObjectFeatureItem getObjectFeatureItem1() {
        return objectFeatureItem1;
    }

    public void setObjectFeatureItem1(ObjectFeatureItem objectFeatureItem1) {
        this.objectFeatureItem1 = objectFeatureItem1;
        if (objectFeatureItem1 != null) {
            this.objectFeatureItemName1 = objectFeatureItem1.getName();
        }
    }

    public Integer getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Integer isSelected) {
        this.isSelected = isSelected;
    }
}
