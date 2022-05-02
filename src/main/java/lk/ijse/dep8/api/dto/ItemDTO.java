package lk.ijse.dep8.api.dto;

public class ItemDTO {
    private String itemCode;
    private String itemName;
    private String price;
    private int quantity;

    public ItemDTO(String itemCode, String itemName, String price, int quantity) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public ItemDTO() {
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ItemDTO{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", price='" + price + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
