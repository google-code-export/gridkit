package org.gridkit.gemfire.search.demo.model;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "beneficiary")
public class Beneficiary implements DataSerializable {
    @XmlElement(name="name")
    private String name;

    @XmlElement(name="coordinator")
    private String coordinator;

    @XmlElement(name="address")
    private String address;

    @XmlElement(name="city")
    private String city;

    @XmlElement(name="post_code")
    private String postCode;

    @XmlElement(name="country")
    private String country;

    @XmlJavaTypeAdapter(AmountXmlAdapter.class)
    @XmlElement(name="detail_amount")
    private Double detailAmount;

    @XmlElement(name="geozone")
    private String geoZone;

    @XmlElement(name="expensetype", required=true)
    private String expenseType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(String coordinator) {
        this.coordinator = coordinator;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getDetailAmount() {
        return detailAmount;
    }

    public void setDetailAmount(Double detailAmount) {
        this.detailAmount = detailAmount;
    }

    public String getGeoZone() {
        return geoZone;
    }

    public void setGeoZone(String geoZone) {
        this.geoZone = geoZone;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    @Override
    public void toData(DataOutput output) throws IOException {
        DataSerializer.writeString(name ,output);
        DataSerializer.writeString(coordinator ,output);
        DataSerializer.writeString(address ,output);
        DataSerializer.writeString(city ,output);
        DataSerializer.writeString(postCode ,output);
        DataSerializer.writeString(country ,output);
        DataSerializer.writeDouble(detailAmount, output);
        DataSerializer.writeString(geoZone ,output);
        DataSerializer.writeString(expenseType ,output);
    }

    @Override
    public void fromData(DataInput input) throws IOException, ClassNotFoundException {
        this.name = DataSerializer.readString(input);
        this.coordinator = DataSerializer.readString(input);
        this.address = DataSerializer.readString(input);
        this.city = DataSerializer.readString(input);
        this.postCode = DataSerializer.readString(input);
        this.country = DataSerializer.readString(input);
        this.detailAmount = DataSerializer.readDouble(input);
        this.geoZone = DataSerializer.readString(input);
        this.expenseType = DataSerializer.readString(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beneficiary that = (Beneficiary) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (coordinator != null ? !coordinator.equals(that.coordinator) : that.coordinator != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        if (detailAmount != null ? !detailAmount.equals(that.detailAmount) : that.detailAmount != null) return false;
        if (expenseType != null ? !expenseType.equals(that.expenseType) : that.expenseType != null) return false;
        if (geoZone != null ? !geoZone.equals(that.geoZone) : that.geoZone != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (postCode != null ? !postCode.equals(that.postCode) : that.postCode != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (coordinator != null ? coordinator.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (detailAmount != null ? detailAmount.hashCode() : 0);
        result = 31 * result + (geoZone != null ? geoZone.hashCode() : 0);
        result = 31 * result + (expenseType != null ? expenseType.hashCode() : 0);
        return result;
    }
}