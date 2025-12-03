package lt.vitalijus.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class SupabaseRestClientConfig(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    @param:Value("\${supabase.service-key}") private val supabaseServiceKey: String
) {


}
