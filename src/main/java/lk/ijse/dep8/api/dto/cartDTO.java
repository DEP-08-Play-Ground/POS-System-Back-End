package lk.ijse.dep8.api.dto;

public class cartDTO {
    private String itemCode;
    private String customerId;
    private String amount;
    private String price;

    public cartDTO(String itemCode, String customerId, String amount, String price) {
        this.itemCode = itemCode;
        this.customerId = customerId;
        this.amount = amount;
        this.price = price;
    }

    public cartDTO() {
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "cartDTO{" +
                "itemCode='" + itemCode + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount='" + amount + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
