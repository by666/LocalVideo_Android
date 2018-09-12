package com.example.CustomXML;

import org.jivesoftware.smack.packet.ExtensionElement;


public class VideoInvitation implements ExtensionElement {
    public static final String NAME_SPACE = "com.webrtc.video";
    public static final String ELEMENT_NAME = "video-chat";

    private String typeElement = "type";

    private String typeText = "";

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
         *</ELEMENT_NAME>*/
        sb.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(NAME_SPACE).append("\">");
        sb.append("<" + typeElement + ">").append(typeText).append("</"+typeElement+">");
        sb.append("</"+ELEMENT_NAME+">");

        return sb.toString();
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }
}
