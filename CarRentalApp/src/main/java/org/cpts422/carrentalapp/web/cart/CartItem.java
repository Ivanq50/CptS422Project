package org.cpts422.carrentalapp.web.cart;

public class CartItem
{
    private CartItemType type;

    private Long vehicleId;
    private String vehicleLabel;
    private Integer days;

    private Long rentalId;

    private Double amount;

    private Double baseAmount;

    private Double discountAmount;
    private Double surchargeAmount;

    // Getters and Setters
    public CartItemType getType() { return type; }
    public void setType(CartItemType type) { this.type = type; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleLabel() { return vehicleLabel; }
    public void setVehicleLabel(String vehicleLabel) { this.vehicleLabel = vehicleLabel; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(Double baseAmount) { this.baseAmount = baseAmount; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getSurchargeAmount() { return surchargeAmount; }
    public void setSurchargeAmount(Double surchargeAmount) { this.surchargeAmount = surchargeAmount; }
}