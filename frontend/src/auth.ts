import Keycloak from 'keycloak-js'
import { ref } from 'vue'

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'twitterclone',
  clientId: 'twitterclone-web',
})

export const currentUser = ref<{ username: string; displayName: string } | null>(null)

export async function initAuth() {
  try {
    await keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: false })
  } catch (e) {
    console.warn('Keycloak not reachable, continuing anonymously', e)
  }
  const token = keycloak.tokenParsed
  if (token) {
    currentUser.value = {
      username: token.preferred_username,
      displayName: token.name ?? token.preferred_username,
    }
  }
}

export const login = () => keycloak.login()

export const logout = () => keycloak.logout({ redirectUri: window.location.origin })

export async function accessToken() {
  await keycloak.updateToken(30)
  return keycloak.token
}
