package hr.mpopijac.electricity_tracker.models;


import javax.persistence.*;
import java.time.Month;

@Entity
@Table(name = "electricity_meter_readings", uniqueConstraints = @UniqueConstraint(columnNames = {"year","month", "electricity_meter_id"}))
public class ElectricityMeterReadingModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Month month;

    @Column(nullable = false)
    private Integer consumedEnergy;

    @ManyToOne
    private ElectricityMeterModel electricityMeter;

    public ElectricityMeterReadingModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Integer getConsumedEnergy() {
        return consumedEnergy;
    }

    public void setConsumedEnergy(Integer consumedEnergy) {
        this.consumedEnergy = consumedEnergy;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ElectricityMeterModel getElectricityMeter() {
        return electricityMeter;
    }

    public void setElectricityMeter(ElectricityMeterModel electricityMeter) {
        this.electricityMeter = electricityMeter;
    }
}
