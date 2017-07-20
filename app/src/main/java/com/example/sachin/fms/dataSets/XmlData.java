package com.example.sachin.fms.dataSets;

/**
 * Created by Sachin on 24,September,2016
 * Hawkinski,
 * Dubai, UAE.
 */
public class XmlData {

    private String emp_name;

    private String cd;
    private String pwd;
    private String mail;
    private String emp_code;
    public String code, description, uom, name;

    public XmlData() {

    }

    public XmlData(String code, String description) {


        this.code = code;
        this.description = description;


    }

    public XmlData(String code, String name, String uom) {


        this.code = code;
        this.name = name;
        this.uom = uom;


    }

    public XmlData(String name, String cd, String pwd, String mail, String emp_code) {


        this.emp_name = name;
        this.cd = cd;
        this.pwd = pwd;
        this.mail = mail;
        this.emp_code = emp_code;


    }
}

