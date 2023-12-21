module phase2.beta5 {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.desktop;
	requires org.bitcoinj.core;
	requires org.bouncycastle.provider;
	requires org.apache.commons.codec;
	requires json.simple;
	requires org.apache.httpcomponents.httpcore;
	requires org.apache.httpcomponents.httpclient;
	requires org.json;

    opens phase2.beta5 to javafx.fxml;
    exports phase2.beta5;
}
