package com.example.CustomXML;

import org.jivesoftware.smack.packet.ExtensionElement;


public class IceCandidateExtensionElement implements ExtensionElement {
    public static final String NAME_SPACE = "com.webrtc.ice_candidate";
    public static final String ELEMENT_NAME = "ice_candidate";

    //代表public final String sdpMid;
    private String sdpMidElement = "sdpMid";
    private String sdpMidText = "";

    //代表public final int sdpMLineIndex;
    private String sdpMLineIndexElement = "sdpMLineIndex";
    private int sdpMLineIndexText = 0;

    //代表public final String sdp;
    private String sdpElement = "sdp";
    private String sdpText = "";

    public String getSdpMidText() {
        return sdpMidText;
    }

    public void setSdpMidText(String sdpMidText) {
        this.sdpMidText = sdpMidText;
    }

    public int getSdpMLineIndexText() {
        return sdpMLineIndexText;
    }

    public void setSdpMLineIndexText(int sdpMLineIndexText) {
        this.sdpMLineIndexText = sdpMLineIndexText;
    }

    public String getSdpText() {
        return sdpText;
    }

    public void setSdpText(String sdpText) {
        this.sdpText = sdpText;
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
         *  <sdpMidElement>sdpMidText</sdpMidElement>
         *  <sdpMLineIndexElement>sdpMLineIndexText</sdpMLineIndexElement>
         *  <sdpElement>sdpText</sdpElement>
         *</ELEMENT_NAME>*/
        sb.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(NAME_SPACE).append("\">");
        sb.append("<" + sdpMidElement + ">").append(sdpMidText).append("</"+sdpMidElement+">");
        sb.append("<" + sdpMLineIndexElement + ">").append(sdpMLineIndexText).append("</"+sdpMLineIndexElement+">");
        sb.append("<" + sdpElement + ">").append(sdpText).append("</"+sdpElement+">");
        sb.append("</"+ELEMENT_NAME+">");

        return sb.toString();
    }
}
