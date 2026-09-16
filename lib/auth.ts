import { NextRequest } from 'next/server'

export function isAuthorized(req: NextRequest) {
  const header = req.headers.get('authorization')
  if (!header?.startsWith('Bearer ')) return false
  const token = header.slice('Bearer '.length)
  return token === process.env.API_TOKEN
}
