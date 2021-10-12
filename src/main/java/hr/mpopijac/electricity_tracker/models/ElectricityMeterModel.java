package hr.mpopijac.electricity_tracker.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "electricity_meters")
public class ElectricityMeterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String serialNumber;

    @OneToOne(mappedBy = "electricityMeter")
    private ClientModel client;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "electricity_meter_id")
    private List<ElectricityMeterReadingModel> electricityMeterReadings = new ArrayList<>();

    public ElectricityMeterModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClientModel getClient() {
        return client;
    }

    public void setClient(ClientModel client) {
        this.client = client;
    }

    public List<ElectricityMeterReadingModel> getElectricityMeterReadings() {
        return electricityMeterReadings;
    }

    public void setElectricityMeterReadings(List<ElectricityMeterReadingModel> electricityMeterReadings) {
        this.electricityMeterReadings = electricityMeterReadings;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
