package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;

import java.util.UUID;

public class VerificationMethodExtension extends ModelOperationExtension<VerificationMethod> {
    static  {
        registerExtension(new VerificationMethodExtension());
    }
    
    @Override
    protected void beforeValidate(VerificationMethod instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getName())) {
            instance.setName(UUID.randomUUID().toString());
        }
        Subject controller = instance.getController();

        if (ObjectUtil.isVoid(instance.getDid())){
            instance.setDid(String.format("%s#%s".formatted(controller.getDid(),instance.getName())));
        }
        
        VerificationMethod.PublicKeyType keyType = PublicKeyType.valueOf(instance.getType());
        
        if (keyType == PublicKeyType.Dns) {
            instance.setPublicKey(String.format("%s.%s", instance.getName(), controller.getName()));
        }
        
        if (!instance.getRawRecord().isNewRecord() && instance.isDirty()){
            boolean beingVerified =  instance.getReflector().getJdbcTypeHelper().getTypeRef(boolean.class).getTypeConverter().valueOf(instance.getTxnProperty("being.verified"));
            if (instance.isVerified() &&  !beingVerified){
                throw new RuntimeException("Cannot change verified methods once  have been validated");
            }
        }
        
        
    }
    
    @Override
    protected void beforeCreate(VerificationMethod instance) {
        super.beforeCreate(instance);
        instance.challenge(false);
        
    }
}
