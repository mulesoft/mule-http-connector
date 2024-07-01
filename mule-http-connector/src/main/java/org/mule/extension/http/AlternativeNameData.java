package org.mule.extension.http;

public class AlternativeNameData {
  private int type;
  private String name;

  public AlternativeNameData(int type, String name) {
    this.type = type;
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public String getName() {
    return name;
  }

}