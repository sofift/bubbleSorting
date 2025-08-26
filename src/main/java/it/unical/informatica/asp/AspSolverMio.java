package it.unical.informatica.asp;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

public class AspSolverMio {
    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String ASP_RULES_FILE = "src/main/resources/asp/rules.asp";
    private static final int DEFAULT_HORIZON = 20;

    private Handler handler;
    private boolean initialized = false;

    public AspSolverMio(){
        this.handler = new DesktopHandler(new DLV2DesktopService(DLV2_PATH));
    }


}
