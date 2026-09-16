import { loadEnvConfig } from '@next/env'
loadEnvConfig(process.cwd())

import { drizzle } from 'drizzle-orm/neon-http'
import { neon } from '@neondatabase/serverless'
import { items, tags } from '../lib/db/schema'

const sql = neon(process.env.DATABASE_URL!)
const db = drizzle(sql)

const DRINKS = ['Beer', 'Sparkling Wine', 'Whiskey', 'Cocktail', 'Coffee']
const FOOD = ['Sausage', 'Brettljause', 'Noodles', 'Gulasch', 'Toast', 'Müsli', 'Salad', 'Vegetarian', 'Restaurant']
const TAGS = ['Home', 'Vacation']

async function main() {
  console.log('Seeding drinks...')
  for (const name of DRINKS) {
    await db.insert(items).values({ name, category: 'drink' }).onConflictDoNothing()
  }
  console.log(`  → ${DRINKS.length} drinks seeded`)

  console.log('Seeding food...')
  for (const name of FOOD) {
    await db.insert(items).values({ name, category: 'food' }).onConflictDoNothing()
  }
  console.log(`  → ${FOOD.length} food items seeded`)

  console.log('Seeding tags...')
  for (const name of TAGS) {
    await db.insert(tags).values({ name }).onConflictDoNothing()
  }
  console.log(`  → ${TAGS.length} tags seeded`)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
