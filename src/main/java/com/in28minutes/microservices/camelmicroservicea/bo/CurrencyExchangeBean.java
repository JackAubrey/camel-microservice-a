package com.in28minutes.microservices.camelmicroservicea.bo;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrencyExchangeBean {
    private Long id;
    private String from;
    private String to;
    private BigDecimal conversionMultiple;

    /**
     *
     */
    public CurrencyExchangeBean() {
    }

    /**
     * @param id
     * @param from
     * @param to
     * @param conversionMultiple
     */
    public CurrencyExchangeBean(Long id, String from, String to, BigDecimal conversionMultiple) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.conversionMultiple = conversionMultiple;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getConversionMultiple() {
        return conversionMultiple;
    }

    public void setConversionMultiple(BigDecimal conversionMultiple) {
        this.conversionMultiple = conversionMultiple;
    }

    @Override
    public String toString() {
        return "CurrencyExchangeBean{" +
                "id=" + id +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", conversionMultiple=" + conversionMultiple +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyExchangeBean that = (CurrencyExchangeBean) o;
        return Objects.equals(id, that.id) && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(conversionMultiple, that.conversionMultiple);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, from, to, conversionMultiple);
    }
}
