package net.yyhis.transact_analyze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.GraphicsEnvironment;

@SpringBootApplication
public class TransactAnalyzeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactAnalyzeApplication.class, args);

		System.out.println("App start!!");

		if (GraphicsEnvironment.isHeadless()) {
            System.out.println("This environment does not support GUI.");
        } else {
            System.out.println("GUI is supported in this environment.");

			// Swing UI 실행
			javax.swing.SwingUtilities.invokeLater(() -> {
				new TransactionAnalyzerApp().setVisible(true); // Swing GUI를 실행
			});
        }
	
	}

}
