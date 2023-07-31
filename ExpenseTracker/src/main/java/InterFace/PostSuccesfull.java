package InterFace;

@FunctionalInterface
public interface PostSuccesfull {
    void onResult(boolean userExists,Throwable error);
}
