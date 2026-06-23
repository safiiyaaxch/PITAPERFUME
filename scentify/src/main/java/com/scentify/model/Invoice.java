package com.scentify.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;  
    
    @Column(unique = true, nullable = false, length = 50)
    private String invoiceNumber;
    
    @OneToOne
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;
    
    @OneToOne
    @JoinColumn(name = "paymentId")
    private Payment payment;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(length = 20)
    private String taxRate = "0%";
    
    @Column(nullable = false)
    private LocalDateTime invoiceDate;
    
    @Column(length = 50)
    private String paymentMethod = "ToyyibPay";
    
    @Column(length = 100)
    private String paymentReference;
    
    @Column(columnDefinition = "TEXT")
    private String invoiceHtml;
    
    // Constructors
    public Invoice() {}
    
    public Invoice(Order order, Payment payment) {
        this.order = order;
        this.payment = payment;
        this.invoiceDate = LocalDateTime.now();
        this.invoiceNumber = generateInvoiceNumber();
        this.subtotal = order.getTotalPrice();
        this.tax = BigDecimal.ZERO;
        this.total = order.getTotalPrice();
        this.paymentReference = payment.getBillCode();
    }
    
    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) 
               + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    // Getters and Setters
    public Long getInvoiceId() { 
        return invoiceId; 
    }
    
    public void setInvoiceId(Long invoiceId) { 
        this.invoiceId = invoiceId; 
    }
    
    public String getInvoiceNumber() { 
        return invoiceNumber; 
    }
    
    public void setInvoiceNumber(String invoiceNumber) { 
        this.invoiceNumber = invoiceNumber; 
    }
    
    public Order getOrder() { 
        return order; 
    }
    
    public void setOrder(Order order) { 
        this.order = order; 
    }
    
    public Payment getPayment() { 
        return payment; 
    }
    
    public void setPayment(Payment payment) { 
        this.payment = payment; 
    }
    
    public BigDecimal getSubtotal() { 
        return subtotal; 
    }
    
    public void setSubtotal(BigDecimal subtotal) { 
        this.subtotal = subtotal; 
    }
    
    public BigDecimal getTax() { 
        return tax; 
    }
    
    public void setTax(BigDecimal tax) { 
        this.tax = tax; 
    }
    
    public BigDecimal getTotal() { 
        return total; 
    }
    
    public void setTotal(BigDecimal total) { 
        this.total = total; 
    }
    
    public String getTaxRate() { 
        return taxRate; 
    }
    
    public void setTaxRate(String taxRate) { 
        this.taxRate = taxRate; 
    }
    
    public LocalDateTime getInvoiceDate() { 
        return invoiceDate; 
    }
    
    public void setInvoiceDate(LocalDateTime invoiceDate) { 
        this.invoiceDate = invoiceDate; 
    }
    
    public String getPaymentMethod() { 
        return paymentMethod; 
    }
    
    public void setPaymentMethod(String paymentMethod) { 
        this.paymentMethod = paymentMethod; 
    }
    
    public String getPaymentReference() { 
        return paymentReference; 
    }
    
    public void setPaymentReference(String paymentReference) { 
        this.paymentReference = paymentReference; 
    }
    
    public String getInvoiceHtml() { 
        return invoiceHtml; 
    }
    
    public void setInvoiceHtml(String invoiceHtml) { 
        this.invoiceHtml = invoiceHtml; 
    }
}
