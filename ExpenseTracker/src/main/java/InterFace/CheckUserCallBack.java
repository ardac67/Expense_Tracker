package InterFace;

@FunctionalInterface
public interface CheckUserCallBack {
    void onResult(boolean userExists,Throwable error);

}
