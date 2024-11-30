package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.exceptions.AccessDeniedException;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import org.apache.lucene.index.DocIDMerger.Sub;

import javax.activation.MimetypesFileTypeMap;
import java.util.UUID;

public class DocumentExtension extends ModelOperationExtension<Document> {
    static {
        registerExtension(new DocumentExtension());
    }
    
    @Override
    protected void beforeValidate(Document instance) {
        super.beforeValidate(instance);
        if (instance.getStreamContentSize() == 0 && instance.getStream() != null){
            try {
                instance.setStreamContentSize(instance.getStream().available());
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        if (ObjectUtil.isVoid(instance.getName())){
            instance.setName(UUID.randomUUID().toString());
        }
        if (instance.getStreamContentType() == null){
            instance.setStreamContentType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(instance.getStreamContentName()));
        }
        if (ObjectUtil.isVoid(instance.getDid())) {
            instance.setDid(String.format("%s/documents/%s",instance.getSubject().getDid(),instance.getName()));
        }
    }
    
    @Override
    protected void beforeSave(Document instance) {
        if (!instance.isDirty()){
            return;
        }
        
        super.beforeSave(instance);
        incrementModCount(instance);
    }
    @Override
    protected void beforeDestroy(Document instance) {
        super.beforeDestroy(instance);
        incrementModCount(instance);
    }
    private void incrementModCount(Document instance){
        Subject subject = instance.getSubject();
        subject.getModCount().increment();
        subject.save();
    }
    
}
