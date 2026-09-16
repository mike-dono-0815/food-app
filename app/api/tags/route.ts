import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { tags } from '@/lib/db/schema'
import { desc } from 'drizzle-orm'

export async function GET(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const all = await db.select().from(tags).orderBy(desc(tags.useCount))
  return NextResponse.json(all)
}

export async function POST(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { name } = await req.json()
  if (!name || typeof name !== 'string') {
    return NextResponse.json({ error: 'name is required' }, { status: 400 })
  }

  try {
    const [created] = await db.insert(tags).values({ name }).returning()
    return NextResponse.json(created)
  } catch (e) {
    if (e instanceof Error && e.message.includes('duplicate key')) {
      return NextResponse.json({ error: 'A tag with this name already exists' }, { status: 409 })
    }
    throw e
  }
}
