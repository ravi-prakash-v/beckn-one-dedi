package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;

import java.util.UUID;

public class ServiceExtension extends ModelOperationExtension<Service> {
    static {
        registerExtension(new ServiceExtension());
    }
    
    @Override
    protected void beforeValidate(Service instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getName())){
            instance.setName(UUID.randomUUID().toString());
        }
        if (ObjectUtil.isVoid(instance.getDid())) {
            instance.setDid(String.format("%s/services/%s",instance.getSubject().getDid(),instance.getName()));
        }
        
    }
    
    @Override
    protected void beforeSave(Service instance) {
        if (!instance.isDirty()){
            return;
        }
        
        super.beforeSave(instance);
        incrementModCount(instance);
    }
    @Override
    protected void beforeDestroy(Service instance) {
        super.beforeDestroy(instance);
        incrementModCount(instance);
    }
    private void incrementModCount(Service instance){
        Subject subject = instance.getSubject();
        subject.getModCount().increment();
        subject.save();
    }
    
}
