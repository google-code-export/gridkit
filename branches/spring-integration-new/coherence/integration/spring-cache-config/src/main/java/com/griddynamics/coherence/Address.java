package com.griddynamics.coherence;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.Base;
import com.tangosol.util.HashHelper;

import java.io.IOException;

public class Address implements PortableObject {
    private String Street1;
    private String Street2;
    private String City;
    private String State;
    private String Zip;
    private String Country;

    /**
     * Default constructor (necessary for PortableObject implementation).
     */
    public Address() {
    }

    public Address(String Street1, String Street2, String City, String State,
                   String Zip, String Country) {
        super();
        this.Street1 = Street1;
        this.Street2 = Street2;
        this.City = City;
        this.State = State;
        this.Zip = Zip;
        this.Country = Country;
    }
//------------ accessors--------------------------------

    public void setStreet1(String Street1) {
        this.Street1 = Street1;
    }

    public String getStreet1() {
        return Street1;
    }

    public void setStreet2(String Street2) {
        this.Street2 = Street2;
    }

    public String getStreet2() {
        return Street2;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getCity() {
        return City;
    }

    public void setState(String State) {
        this.State = State;
    }

    public String getState() {
        return State;
    }

    public void setZip(String Zip) {
        this.Zip = Zip;
    }

    public String getZip() {
        return Zip;
    }

    public void setCountry(String Country) {
        this.Country = Country;
    }

    public String getCountry() {
        return Country;
    }
// -------- PortableObject Interface------------------------------

    public void readExternal(PofReader reader)
            throws IOException {
        setStreet1(reader.readString(0));
        setStreet2(reader.readString(1));
        setCity(reader.readString(2));
        setState(reader.readString(3));
        setZip(reader.readString(4));
        setCountry(reader.readString(5));
    }

    public void writeExternal(PofWriter writer)
            throws IOException {
        writer.writeString(0, getStreet1());
        writer.writeString(1, getStreet2());
        writer.writeString(2, getCity());
        writer.writeString(3, getState());
        writer.writeString(4, getZip());
        writer.writeString(5, getCountry());
    }
// ----- Object methods --------------------------------------------------

    public boolean equals(Object oThat) {
        if (this == oThat) {
            return true;
        }
        if (oThat == null) {
            return false;
        }
        Address that = (Address) oThat;
        return Base.equals(getStreet1(), that.getStreet1()) &&
                Base.equals(getStreet2(), that.getStreet2()) &&
                Base.equals(getCity(), that.getCity()) &&
                Base.equals(getState(), that.getState()) &&
                Base.equals(getZip(), that.getZip()) &&
                Base.equals(getCountry(), that.getCountry());
    }

    public int hashCode() {
        return HashHelper.hash(getStreet1(),
                HashHelper.hash(getStreet2(),
                        HashHelper.hash(getZip(), 0)));
    }

    public String toString() {
        return getStreet1() + "\n" +
                getStreet2() + "\n" +
                getCity() + ", " + getState() + " " + getZip() + "\n" +
                getCountry();
    }
}
