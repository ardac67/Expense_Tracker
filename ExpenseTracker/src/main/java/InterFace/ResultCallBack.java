package InterFace;

import org.bson.Document;

import java.util.List;

public interface ResultCallBack {
        void handleResult(List<Document> documents);

}
