package application;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
	 private int id;
	 private String title;
	 private BigDecimal amount;
	 private LocalDate date;
	 private String category;
	 private String paymentMethod;
	 private String notes;
	 private String receiptPath;
	 
	 
	 
	 public Expense() {
		super();
		// TODO Auto-generated constructor stub
	}



	 public Expense(String title, BigDecimal amount, LocalDate date, String category,
	             String paymentMethod, String notes, String receiptPath) {
	  this.title = title;
	  this.amount = amount;
	  this.date = date;
	  this.category = category;
	  this.paymentMethod = paymentMethod;
	  this.notes = notes;
	  this.receiptPath = receiptPath;
	}




	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public BigDecimal getAmount() {
		return amount;
	}



	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}



	public LocalDate getDate() {
		return date;
	}



	public void setDate(LocalDate date) {
		this.date = date;
	}



	public String getCategory() {
		return category;
	}



	public void setCategory(String category) {
		this.category = category;
	}



	public String getPaymentMethod() {
		return paymentMethod;
	}



	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}



	public String getNotes() {
		return notes;
	}



	public void setNotes(String notes) {
		this.notes = notes;
	}



	public String getReceiptPath() {
		return receiptPath;
	}



	public void setReceiptPath(String receiptPath) {
		this.receiptPath = receiptPath;
	}



	@Override
	public String toString() {
		return "Expense [id=" + id + ", title=" + title + ", amount=" + amount + ", date=" + date + ", category="
				+ category + ", paymentMethod=" + paymentMethod + ", notes=" + notes + ", receiptPath=" + receiptPath
				+ "]";
	}
	
	
	 
}
