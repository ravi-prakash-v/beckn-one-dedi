package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.fs.Directory;
import in.succinct.defs.db.model.did.fs.Record;

import java.util.UUID;

public class RecordExtension extends ModelOperationExtension<Record>{
    static {
        registerExtension(new RecordExtension());
    }
    
    @Override
    protected void beforeValidate(Record instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getName())){
            instance.setName(UUID.randomUUID().toString());
        }
        if (ObjectUtil.isVoid(instance.getDid())) {
            instance.setDid(String.format("%s/records/%s",instance.getDirectory().getDid(),instance.getName()));
        }
    }
}
