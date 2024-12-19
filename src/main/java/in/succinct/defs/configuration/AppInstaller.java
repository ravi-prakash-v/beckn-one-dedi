package in.succinct.defs.configuration;

import com.venky.swf.configuration.Installer;
import com.venky.swf.db.Database;
import in.succinct.defs.util.KeyManager;

public class AppInstaller implements Installer {

    public void install() {
        Database.getInstance().resetIdGeneration();
        KeyManager.getInstance().generateInitialKeys();
        
    }

   

}

