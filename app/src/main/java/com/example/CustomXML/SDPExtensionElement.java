package com.example.CustomXML;

import org.jivesoftware.smack.packet.ExtensionElement;

public class SDPExtensionElement implements ExtensionElement {
    public static final String NAME_SPACE = "com.webrtc.sdp";
    public static final String ELEMENT_NAME = "sdp";

    //代表SessionDescription.Type type
    private String typeElement = "type";
    private String typeText = "";

    //代表 String description
    private String descriptionElement = "description";
    private String descriptionText = "";

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    @Override
    public String getNamespace() {
        return NAME_SPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public CharSequence toXML() {
        StringBuilder sb = new StringBuilder();

        /*<ELEMENT_NAMW xmlns=NAME_SAPCE>
         *  <typeElement>typeText<typeElement>
         *  <descriptionElement>descriptionText</descriptionElement>
         *</ELEMENT_NAME>*/
        sb.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(NAME_SPACE).append("\">");
        sb.append("<" + typeElement + ">").append(typeText).append("</"+typeElement+">");
        sb.append("<" + descriptionElement + ">").append(descriptionText).append("</"+descriptionElement+">");
        sb.append("</"+ELEMENT_NAME+">");

        return sb.toString();
    }
}
