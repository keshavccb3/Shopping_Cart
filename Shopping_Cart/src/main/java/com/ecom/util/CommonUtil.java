package com.ecom.util;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom("agg.keshav90@gmail.com", "Shopping Cart");
		helper.setTo(reciepentEmail);

		String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password : </p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		helper.setSubject("Reset password");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		String siteUrl = request.getRequestURL().toString();
		return siteUrl.replace(request.getServletPath(), "");
	}

	String msg = null;

	public Boolean sendMailForProductOrder(ProductOrder order, String st)
	        throws UnsupportedEncodingException, MessagingException {
	    
	    String recipientEmail = order.getOrderAddress().getEmail();

	    if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
	        System.err.println("Email address is missing for order ID: " + order.getId());
	        return false;
	    }

	    msg = "<p><b>[[orderStatus]]</b></p>"
	            + "<p><b>Product Details:<b></p>"
	            + "<p>Name: [[productName]]</p>"
	            + "<p>Category: [[category]]</p>"
	            + "<p>Quantity: [[quantity]]</p>"
	            + "<p>Price: [[price]]</p>"
	            + "<p>Payment Type: [[paymentType]]</p>";

	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message);
	    helper.setFrom("agg.keshav90@gmail.com", "Shopping Cart");
	    helper.setTo(recipientEmail);

	    Double totalPrice = order.getPrice() * order.getQuantity();
	    msg = msg.replace("[[orderStatus]]", st);
	    msg = msg.replace("[[productName]]", order.getProduct().getTitle());
	    msg = msg.replace("[[category]]", order.getProduct().getCategory());
	    msg = msg.replace("[[quantity]]", order.getQuantity().toString());
	    msg = msg.replace("[[price]]", totalPrice.toString());
	    msg = msg.replace("[[paymentType]]", order.getPaymentType());

	    helper.setSubject("Product Order Status");
	    helper.setText(msg, true);
	    mailSender.send(message);
	    return true;
	}

}
