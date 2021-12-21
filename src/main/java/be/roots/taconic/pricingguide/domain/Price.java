package be.roots.taconic.pricingguide.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Price {

    private String quantity;
    private String price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Price{" +
                "quantity='" + quantity + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
