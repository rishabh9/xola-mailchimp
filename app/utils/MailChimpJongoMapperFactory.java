package utils;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import uk.co.panaxiom.playjongo.JongoMapperFactory;

/**
 * Appends JDK 8 Support for Date & Time to the Jackson Mapper.
 *
 * @author rishabh
 */
public class MailChimpJongoMapperFactory implements JongoMapperFactory {

    @Override
    public Mapper create() {
        JacksonMapper.Builder builder = new JacksonMapper.Builder();
        ObjectMapper.findModules().forEach(builder::registerModule);
        builder.enable(MapperFeature.AUTO_DETECT_GETTERS);
        builder.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
        return builder.build();
    }
}
