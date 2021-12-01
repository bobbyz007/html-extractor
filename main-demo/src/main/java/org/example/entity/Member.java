package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Member {
    public static final String FIELD_EMAIL = "Email";
    public static final String FIELD_ADDRESS = "Address";
    public static final String FIELD_TELEPHONE = "Telephone";
    public static final String FIELD_WEB = "Web";
    public static final String FIELD_TWITTER = "Twitter";
    public static final String FIELD_CONTACTS = "Contacts";
    private String type;
    private String title;
    private String desc;

    private String address;
    private String telephone;
    private String email;
    private String webUrl;
    private String twitterAccount;
    private String contacts;

    public Member(String type, String title, String desc) {
        this.type = type;
        this.title = title;
        this.desc = desc;
    }

    public static Member[] mock(int size) {
        Member[] members = new Member[size];
        for (int i = 0; i < size; i++) {
            Member m = new Member("type " + i, "title " + i, "desc " + i);
            members[i] = m;

            m.setAddress("address " + i);
            m.setTelephone("telephone " + i);
            m.setEmail("mailto:xxx@163.com");
            m.setWebUrl("web url " + i);
            m.setTwitterAccount("twitter " + i);
            m.setContacts("contacts " + i);
        }

        return members;
    }

    public void setValue(String name, String value) {
        switch (name) {
            case FIELD_ADDRESS:
                setAddress(value);
                break;
            case FIELD_TELEPHONE:
                setTelephone(value);
                break;
            case FIELD_EMAIL:
                setEmail(value);
                break;
            case FIELD_WEB:
                setWebUrl(value);
                break;
            case FIELD_TWITTER:
                setTwitterAccount(value);
                break;
            case FIELD_CONTACTS:
                setContacts(value);
        }
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getTwitterAccount() {
        return twitterAccount;
    }

    public void setTwitterAccount(String twitterAccount) {
        this.twitterAccount = twitterAccount;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }
}
