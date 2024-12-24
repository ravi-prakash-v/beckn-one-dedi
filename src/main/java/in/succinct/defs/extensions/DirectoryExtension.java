package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.fs.Directory;

import java.util.UUID;

public class DirectoryExtension extends ModelOperationExtension<Directory>{
    static {
        registerExtension(new DirectoryExtension());
    }
    
    @Override
    protected void beforeValidate(Directory instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getName())){
            instance.setName(UUID.randomUUID().toString());
        }
        if (ObjectUtil.isVoid(instance.getDid())) {
            instance.setDid(String.format("/directories/%s",instance.getName()));
        }
    }
}
