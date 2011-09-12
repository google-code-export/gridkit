package org.gridkit.gemfire.search.demo.model;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "commitment")
public class Commitment implements DataSerializable {
    @XmlElement(name="year")
    private Integer year;

    @XmlJavaTypeAdapter(AmountXmlAdapter.class)
    @XmlElement(name="amount")
    private Double amount;

    @XmlElement(name="position_key")
    private String positionKey;

    @XmlElement(name="grant_subject")
    private String grantSubject;

    @XmlElement(name="responsible_department")
    private String responsibleDepartment;

    @XmlElement(name="budget_line")
    private String budgetLine;

    @XmlElement(name="actiontype")
    private String actionType;

    @XmlElement(name="cofinancing_rate")
    private String cofinancingRate;

    @XmlElementWrapper(name="beneficiaries")
    @XmlElement(name="beneficiary")
    private List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPositionKey() {
        return positionKey;
    }

    public void setPositionKey(String positionKey) {
        this.positionKey = positionKey;
    }

    public String getGrantSubject() {
        return grantSubject;
    }

    public void setGrantSubject(String grantSubject) {
        this.grantSubject = grantSubject;
    }

    public String getResponsibleDepartment() {
        return responsibleDepartment;
    }

    public void setResponsibleDepartment(String responsibleDepartment) {
        this.responsibleDepartment = responsibleDepartment;
    }

    public String getBudgetLine() {
        return budgetLine;
    }

    public void setBudgetLine(String budgetLine) {
        this.budgetLine = budgetLine;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public List<Beneficiary> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<Beneficiary> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public String getCofinancingRate() {
        return cofinancingRate;
    }

    public void setCofinancingRate(String cofinancingRate) {
        this.cofinancingRate = cofinancingRate;
    }

    @Override
    public void toData(DataOutput output) throws IOException {
        DataSerializer.writeInteger(year, output);
        DataSerializer.writeDouble(amount ,output);
        DataSerializer.writeString(positionKey ,output);
        DataSerializer.writeString(grantSubject ,output);
        DataSerializer.writeString(responsibleDepartment ,output);
        DataSerializer.writeString(budgetLine ,output);
        DataSerializer.writeString(actionType, output);
        DataSerializer.writeString(cofinancingRate ,output);
        DataSerializer.writeObjectArray(beneficiaries.toArray(), output);
    }

    @Override
    public void fromData(DataInput input) throws IOException, ClassNotFoundException {
        this.year = DataSerializer.readInteger(input);
        this.amount = DataSerializer.readDouble(input);
        this.positionKey = DataSerializer.readString(input);
        this.grantSubject = DataSerializer.readString(input);
        this.responsibleDepartment = DataSerializer.readString(input);
        this.budgetLine = DataSerializer.readString(input);
        this.actionType = DataSerializer.readString(input);
        this.cofinancingRate = DataSerializer.readString(input);

        Beneficiary[] beneficiariesArray = (Beneficiary[])DataSerializer.readObjectArray(input);
        this.beneficiaries = new ArrayList<Beneficiary>(Arrays.asList(beneficiariesArray));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commitment that = (Commitment) o;

        if (actionType != null ? !actionType.equals(that.actionType) : that.actionType != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (beneficiaries != null ? !beneficiaries.equals(that.beneficiaries) : that.beneficiaries != null)
            return false;
        if (budgetLine != null ? !budgetLine.equals(that.budgetLine) : that.budgetLine != null) return false;
        if (cofinancingRate != null ? !cofinancingRate.equals(that.cofinancingRate) : that.cofinancingRate != null)
            return false;
        if (grantSubject != null ? !grantSubject.equals(that.grantSubject) : that.grantSubject != null) return false;
        if (positionKey != null ? !positionKey.equals(that.positionKey) : that.positionKey != null) return false;
        if (responsibleDepartment != null ? !responsibleDepartment.equals(that.responsibleDepartment) : that.responsibleDepartment != null)
            return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year != null ? year.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (positionKey != null ? positionKey.hashCode() : 0);
        result = 31 * result + (grantSubject != null ? grantSubject.hashCode() : 0);
        result = 31 * result + (responsibleDepartment != null ? responsibleDepartment.hashCode() : 0);
        result = 31 * result + (budgetLine != null ? budgetLine.hashCode() : 0);
        result = 31 * result + (actionType != null ? actionType.hashCode() : 0);
        result = 31 * result + (cofinancingRate != null ? cofinancingRate.hashCode() : 0);
        result = 31 * result + (beneficiaries != null ? beneficiaries.hashCode() : 0);
        return result;
    }
}
