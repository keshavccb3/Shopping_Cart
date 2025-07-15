package com.ecom.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String orderId;
	private LocalDate orderDate;
	@ManyToOne
	private Product product;
	@ManyToOne
	private UserDtls user;
	private Double price;
	private Integer quantity;
	private String status;
	private String paymentType;
	@OneToOne(cascade = CascadeType.ALL)
	private OrderAddress orderAddress;
}
