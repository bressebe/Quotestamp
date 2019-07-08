package quotestamp.service

import quotestamp.dao.PlaylistDAO
import quotestamp.entity.Playlist
import quotestamp.factory.PlaylistFactory
import spock.lang.Specification

class DataServiceTest extends Specification {
    def dataService

    def setup() {
        dataService = new DataService()
    }

    def 'a playlist is found by id or created from the playlist dao'() {
        given:
            def playlist = Mock(Playlist)
            def optional = GroovyMock(Optional)
            dataService.playlistFactory = Mock(PlaylistFactory) {
                1 * create("foo") >> playlist
                0 * _
            }
            dataService.playlistDAO = Mock(PlaylistDAO) {
                1 * findById("foo") >> optional
                0 * _
            }
        expect:
            playlist == dataService.findOrCreatePlaylist("foo")
    }

    def 'playlists are saved with the playlist dao'() {
        given:
            def playlist = Mock(Playlist)
            def playlistDAO = Mock(PlaylistDAO)
            dataService.playlistDAO = playlistDAO
        when:
            dataService.savePlaylist(playlist)
        then:
            1 * playlistDAO.save(playlist)
            0 * _
    }
}
