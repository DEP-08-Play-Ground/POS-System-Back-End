package lk.ijse.dep8.api.dto;

import java.util.Arrays;

public class itemDTO {
    private String itemCode;
    private String itemName;
    private String price;
    private String qty;
    private byte[] preview;

    public itemDTO(String itemCode, String itemName, String price, String qty, byte[] preview) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.price = price;
        this.qty = qty;
        this.preview = preview;
    }

    public itemDTO(String itemCode, String itemName, String price, String qty) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.price = price;
        this.qty = qty;
    }

    public itemDTO() {
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

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "itemDTO{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", price='" + price + '\'' +
                ", qty=" + qty +
                ", preview=" + Arrays.toString(preview) +
                '}';
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }
}
