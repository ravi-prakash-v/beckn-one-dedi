package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.path._IPath;
import in.succinct.defs.db.model.did.subject.Subject;
import org.apache.lucene.index.DocIDMerger.Sub;

public class SubjectExtension extends ModelOperationExtension<Subject> {
    static  {
        registerExtension(new SubjectExtension());
    }
    
    @Override
    protected void beforeValidate(Subject instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getDid())){
            _IPath path = Database.getInstance().getContext(_IPath.class.getName());
            
            instance.setDid(String.format("%s/%s".formatted(
                    path.controllerPathElement(),
                    instance.getName())));
            
            Subject persisted = Database.getTable(Subject.class).getRefreshed(instance);
            instance.getRawRecord().load(persisted.getRawRecord());
            instance.getRawRecord().setNewRecord(persisted.getRawRecord().isNewRecord());
        }
    }
    
}
