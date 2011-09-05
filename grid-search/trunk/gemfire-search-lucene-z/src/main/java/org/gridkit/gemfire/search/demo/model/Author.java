package org.gridkit.gemfire.search.demo.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;

public class Author implements DataSerializable {
    private Integer id;
    private String name;
    private Date birthday;

    public Author() {}

    public Author(int id, String name, Date birthday) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return this.birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public void toData(DataOutput output) throws IOException {
        DataSerializer.writeInteger(id, output);
        DataSerializer.writeString(name, output);
        DataSerializer.writeDate(birthday, output);
    }

    @Override
    public void fromData(DataInput input) throws IOException, ClassNotFoundException {
        this.id = DataSerializer.readInteger(input);
        this.name = DataSerializer.readString(input);
        this.birthday = DataSerializer.readDate(input);
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}
