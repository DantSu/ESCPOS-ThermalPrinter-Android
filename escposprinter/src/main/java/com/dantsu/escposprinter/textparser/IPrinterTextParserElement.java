package com.dantsu.escposprinter.textparser;

import com.dantsu.escposprinter.EscPosPrinterCommands;

public interface IPrinterTextParserElement {
    int length();
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket);
}
