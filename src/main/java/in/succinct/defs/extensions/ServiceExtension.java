package in.succinct.defs.extensions;

import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;

public class ServiceExtension extends ModelOperationExtension<Service> {
    static {
        registerExtension(new ServiceExtension());
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
