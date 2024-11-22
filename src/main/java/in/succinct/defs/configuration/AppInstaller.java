package in.succinct.defs.configuration;

import com.venky.swf.configuration.Installer;
import in.succinct.defs.util.KeyManager;

public class AppInstaller implements Installer {

    public void install() {
        KeyManager.getInstance().generateInitialKeys();
        
    }

   

}

