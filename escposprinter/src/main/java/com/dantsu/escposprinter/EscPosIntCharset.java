package com.dantsu.escposprinter;

public class EscPosIntCharset {
  private byte[] intCharsetCommand;

  /**
   * Create new instance of EscPosIntCharset.
   *
   * @param intCharsetId Id of international charset for your printer (Ex: 0)
   */
  public EscPosIntCharset(int intCharsetId) {
    this.intCharsetCommand = new byte[]{0x1B, 0x52, (byte) intCharsetId};
  }

  public byte[] getCommand() {
    return this.intCharsetCommand;
  }
}
