package com.dantsu.escposprinter.textparser;

import java.util.Hashtable;

public class PrinterTextParserTag {
    
    private String tagName = "";
    private Hashtable<String,String> attributes = new Hashtable<String,String>();
    private int length = 0;
    private boolean isCloseTag = false;
    
    public PrinterTextParserTag(String tag) {
        tag = tag.trim();
        
        if(!tag.substring(0, 1).equals("<") || !tag.substring(tag.length() - 1).equals(">")) {
            return;
        }
        
        this.length = tag.length();
        int openTagIndex = tag.indexOf("<"),
            closeTagIndex = tag.indexOf(">"),
            nextSpaceIndex = tag.indexOf(" ");
        
        if(nextSpaceIndex != -1 && nextSpaceIndex < closeTagIndex) {
            this.tagName = tag.substring(openTagIndex + 1, nextSpaceIndex).toLowerCase();
    
            String attributesString = tag.substring(nextSpaceIndex, closeTagIndex).trim();
            while (attributesString.contains("='")) {
                int egalPos = attributesString.indexOf("='"), endPos = attributesString.indexOf("'", egalPos + 2);
        
                String attributeName = attributesString.substring(0, egalPos);
                String attributeValue = attributesString.substring(egalPos + 2, endPos);
        
                if(!attributeName.equals("")) {
                    this.attributes.put(attributeName, attributeValue);
                }
        
                attributesString = attributesString.substring(endPos + 1).trim();
            }
        } else {
            this.tagName = tag.substring(openTagIndex + 1, closeTagIndex).toLowerCase();
        }
        
        
        if(this.tagName.substring(0, 1).equals("/")) {
            this.tagName = this.tagName.substring(1);
            this.isCloseTag = true;
        }
    }
    
    public String getTagName() {
        return this.tagName;
    }
    
    public Hashtable<String, String> getAttributes() {
        return this.attributes;
    }
    
    public String getAttribute(String key) {
        return this.attributes.get(key);
    }
    public boolean hasAttribute(String key) {
        return this.attributes.containsKey(key);
    }
    
    public int getLength() {
        return this.length;
    }
    
    public boolean isCloseTag() {
        return this.isCloseTag;
    }
}
